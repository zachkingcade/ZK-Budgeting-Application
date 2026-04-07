import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { UserHttpClientService } from '../client/user-http-client.service';
import { UserAdapterLoggerService } from '../logging/user-adapter-logger.service';
import { ApiResponseDto } from '../dto/api-response.dto';
import { LoginUserRequestDto } from '../dto/user/login-user-request.dto';
import { LoginUserResponseDto } from '../dto/user/login-user-response.dto';
import { RegisterUserRequestDto } from '../dto/user/register-user-request.dto';
import { RegisterUserResponseDto } from '../dto/user/register-user-response.dto';
import { RefreshLoginRequestDto } from '../dto/user/refresh-login-request.dto';
import { RefreshLoginResponseDto } from '../dto/user/refresh-login-response.dto';
import { LogoutUserRequestDto } from '../dto/user/logout-user-request.dto';

@Injectable({
  providedIn: 'root',
})
export class UserAuthApi {
  constructor(
    private readonly userClient: UserHttpClientService,
    private readonly logger: UserAdapterLoggerService,
  ) {}

  registerUser(request: RegisterUserRequestDto): Observable<ApiResponseDto<RegisterUserResponseDto>> {
    const operation: string = 'UserAuthApi.registerUser /user/register';
    const startTime: number = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.userClient.post<ApiResponseDto<RegisterUserResponseDto>>('/user/register', request).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData,
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, request);
        return throwError(() => error);
      }),
    );
  }

  loginUser(request: LoginUserRequestDto): Observable<ApiResponseDto<LoginUserResponseDto>> {
    const operation: string = 'UserAuthApi.loginUser /user/login';
    const startTime: number = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.userClient.post<ApiResponseDto<LoginUserResponseDto>>('/user/login', request).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData,
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, request);
        return throwError(() => error);
      }),
    );
  }

  refreshLogin(request: RefreshLoginRequestDto): Observable<ApiResponseDto<RefreshLoginResponseDto>> {
    const operation: string = 'UserAuthApi.refreshLogin /user/refresh';
    const startTime: number = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.userClient.post<ApiResponseDto<RefreshLoginResponseDto>>('/user/refresh', request).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData,
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, request);
        return throwError(() => error);
      }),
    );
  }

  logoutUser(request: LogoutUserRequestDto): Observable<ApiResponseDto<string>> {
    const operation: string = 'UserAuthApi.logoutUser /user/logout';
    const startTime: number = Date.now();
    this.logger.debug(`Starting ${operation}`, request);
    return this.userClient.post<ApiResponseDto<string>>('/user/logout', request).pipe(
      tap((response) => {
        this.logger.debug(`Ending ${operation} (${Date.now() - startTime}ms)`, {
          statusMessage: response.statusMessage,
          metaData: response.metaData,
        });
      }),
      catchError((error) => {
        this.logger.error(`Failed ${operation} (${Date.now() - startTime}ms)`, error, request);
        return throwError(() => error);
      }),
    );
  }
}
