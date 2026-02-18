package org.eclipse.slm.common.aas.repositories.submodels;

import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.Result;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMessage;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResult;
import org.eclipse.slm.common.aas.model.submodelrepository.exceptions.SubmodelNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class SubmodelRepositoryRestControllerExceptionHandler {

    @ExceptionHandler(SubmodelNotFoundException.class)
    public ResponseEntity<Result> handleSubmodelNotFound(SubmodelNotFoundException exception) {
        var result = new DefaultResult.Builder().messages(
                new DefaultMessage.Builder()
                        .code("NotFound")
                        .messageType(MessageTypeEnum.ERROR)
                        .correlationId(null)
                        .text(exception.getMessage())
                        .timestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")))
                    .build()
        ).build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
    }

}
