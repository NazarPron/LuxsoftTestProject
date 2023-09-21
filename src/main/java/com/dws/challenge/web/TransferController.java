package com.dws.challenge.web;

import com.dws.challenge.domain.TransferDetails;
import com.dws.challenge.exception.TransferException;
import com.dws.challenge.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/transfer")
@Slf4j
public class TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> startTransfer(@RequestBody @Valid TransferDetails transferDetails) {
        log.info("Starting transfer {}", transferDetails);
        try {
            this.transferService.transfer(transferDetails);
        } catch (TransferException tfe) {
            return new ResponseEntity<>(tfe.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

}