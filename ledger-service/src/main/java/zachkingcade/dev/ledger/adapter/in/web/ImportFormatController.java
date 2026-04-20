package zachkingcade.dev.ledger.adapter.in.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zachkingcade.dev.ledger.adapter.in.web.dto.ApiResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.MetaData;
import zachkingcade.dev.ledger.adapter.in.web.dto.importformat.GetAllImportFormatsResponse;
import zachkingcade.dev.ledger.adapter.in.web.dto.importformat.ImportFormatObject;
import zachkingcade.dev.ledger.application.port.in.importformat.GetAllImportFormatsUseCase;
import zachkingcade.dev.ledger.domain.importformat.ImportFormat;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/importformats")
public class ImportFormatController {

    private static final Logger log = LoggerFactory.getLogger(ImportFormatController.class);

    private final GetAllImportFormatsUseCase getAllImportFormatsUseCase;

    public ImportFormatController(GetAllImportFormatsUseCase getAllImportFormatsUseCase) {
        this.getAllImportFormatsUseCase = getAllImportFormatsUseCase;
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<GetAllImportFormatsResponse>> getAll(@AuthenticationPrincipal Jwt jwt) {
        try {
            // jwt is required by security config, but the list is system-wide
            log.debug("Starting Rest Controller /importformats endpoint /all");
            List<ImportFormat> list = getAllImportFormatsUseCase.getAllActiveImportFormats();

            List<ImportFormatObject> resulting = new ArrayList<>();
            for (ImportFormat f : list) {
                resulting.add(new ImportFormatObject(
                        f.id(),
                        f.formatName(),
                        f.formatType(),
                        f.formatDetails(),
                        f.active()
                ));
            }

            GetAllImportFormatsResponse response = new GetAllImportFormatsResponse(resulting);
            ApiResponse<GetAllImportFormatsResponse> apiResponse = new ApiResponse<>(
                    String.format("Returned [%s] Import Formats", resulting.size()),
                    new MetaData((long) resulting.size()),
                    response
            );
            log.debug("Ending Rest Controller /importformats endpoint /all count:[{}]", resulting.size());
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (RuntimeException ex) {
            log.error("ImportFormatController.getAll failed", ex);
            throw ex;
        }
    }
}

