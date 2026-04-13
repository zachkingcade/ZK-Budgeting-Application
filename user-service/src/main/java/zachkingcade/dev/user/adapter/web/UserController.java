package zachkingcade.dev.user.adapter.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import zachkingcade.dev.user.adapter.web.dto.ApiResponse;
import zachkingcade.dev.user.adapter.web.dto.MetaData;
import zachkingcade.dev.user.adapter.web.dto.user.*;
import zachkingcade.dev.user.application.commands.ServiceLoginCommand;
import zachkingcade.dev.user.application.commands.LoginUserCommand;
import zachkingcade.dev.user.application.commands.LogoutUserCommand;
import zachkingcade.dev.user.application.commands.RefreshSessionCommand;
import zachkingcade.dev.user.application.commands.RegisterUserCommand;
import zachkingcade.dev.user.application.port.in.Session.RefreshSessionUseCase;
import zachkingcade.dev.user.application.port.in.user.LoginUserUseCase;
import zachkingcade.dev.user.application.port.in.user.LogoutUserUseCase;
import zachkingcade.dev.user.application.port.in.service.ServiceLoginUseCase;
import zachkingcade.dev.user.application.port.in.user.RegisterUserUseCase;
import zachkingcade.dev.user.application.results.ServiceLoginResult;
import zachkingcade.dev.user.application.results.LogInUserResult;
import zachkingcade.dev.user.application.results.RefreshSessionResult;
import zachkingcade.dev.user.domain.session.Session;
import zachkingcade.dev.user.domain.user.User;

@RestController()
@RequestMapping("/user")
public class UserController {

    RegisterUserUseCase registerUserUseCase;
    LoginUserUseCase loginUserUseCase;
    LogoutUserUseCase logoutUserUseCase;
    RefreshSessionUseCase refreshSessionUseCase;
    ServiceLoginUseCase serviceLoginUseCase;

    public UserController(
            RegisterUserUseCase registerUserUseCase,
            LoginUserUseCase loginUserUseCase,
            LogoutUserUseCase logoutUserUseCase,
            RefreshSessionUseCase refreshSessionUseCase,
            ServiceLoginUseCase serviceLoginUseCase
    ) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUserUseCase = loginUserUseCase;
        this.logoutUserUseCase = logoutUserUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.serviceLoginUseCase = serviceLoginUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterUserResponse>> registerUser(@RequestBody RegisterUserRequest request){
        RegisterUserCommand command = new RegisterUserCommand(request.username(), request.password());
        User newUser = registerUserUseCase.registerUser(command);
        RegisterUserResponse response = new RegisterUserResponse(newUser.getUsername());
        ApiResponse<RegisterUserResponse> apiResponse = new ApiResponse<>(String.format("Created User [%s]",newUser.getUsername()), new MetaData(1L), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginUserResponse>> loginUser(@RequestBody LoginUserRequest request){
        LoginUserCommand command = new LoginUserCommand(request.username(), request.password());
        LogInUserResult applicationResult = loginUserUseCase.loginUser(command);
        LoginUserResponse response = new LoginUserResponse(applicationResult.username(), applicationResult.sessionToken(), applicationResult.sessionCreatedAt(), applicationResult.sessionExpiresAt(), applicationResult.accessToken(), applicationResult.accessTokenCreatedAt(), applicationResult.AccessTokenExpiresAt());
        ApiResponse<LoginUserResponse> apiResponse = new ApiResponse<>(String.format("User [%s] Logged into system successfully",request.username()), new MetaData(1L), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.ACCEPTED);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser(@RequestBody LogoutUserRequest request){
        LogoutUserCommand command = new LogoutUserCommand(request.username(), request.sessionToken());
        logoutUserUseCase.logoutUser(command);
        String response = request.username();
        ApiResponse<String> apiResponse = new ApiResponse<>(String.format("User [%s] Logged out of system successfully",request.username()), new MetaData(1L), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/service/login")
    public ResponseEntity<ApiResponse<ServiceLoginResponse>> serviceLogin(@Valid @RequestBody ServiceLoginRequest request) {
        ServiceLoginCommand command = new ServiceLoginCommand(
                request.serviceName(),
                request.secret(),
                request.actingForUserId(),
                request.audiences(),
                request.scopes()
        );
        ServiceLoginResult result = serviceLoginUseCase.loginService(command);
        ServiceLoginResponse response = new ServiceLoginResponse(
                result.accessToken(),
                result.accessTokenCreatedAt(),
                result.accessTokenExpiresAt()
        );
        ApiResponse<ServiceLoginResponse> apiResponse = new ApiResponse<>(
                String.format("Service [%s] authenticated", request.serviceName()),
                new MetaData(1L),
                response
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshLoginResponse>> refreshUser(@RequestBody RefreshLoginRequest request){
        RefreshSessionCommand command = new RefreshSessionCommand(request.username(), request.sessionToken());
        RefreshSessionResult result = refreshSessionUseCase.refreshSession(command);
        RefreshLoginResponse response = new RefreshLoginResponse(result.accessToken(), result.accessTokenCreatedAt(), result.AccessTokenExpiresAt());
        String statusMessage = result.sessionRefreshed()? "Session Refreshed, new accessToken provided." : "Session expired, please login again.";
        ApiResponse<RefreshLoginResponse> apiResponse = new ApiResponse<>(String.format(statusMessage,request.username()), new MetaData(1L), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
