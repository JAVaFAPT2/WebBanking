
package account.fundtransfer.controller;


import account.fundtransfer.models.dto.FundTranferDTO;
import account.fundtransfer.models.dto.request.FundTransferRequest;
import account.fundtransfer.models.dto.response.FundTranferResponse;
import account.fundtransfer.service.FundTranferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
@RequestMapping("/fund-transfers")
public class RestController {
    private final FundTranferService  fundTranferService;
    @PostMapping
    public ResponseEntity<FundTranferResponse> fundTranfer (@RequestBody FundTransferRequest fundTransferRequest) {
            return new ResponseEntity<>(fundTranferService.fundTranfer(fundTransferRequest), HttpStatus.CREATED);
    }
    @GetMapping("/{referenceId}")
    public ResponseEntity<FundTranferDTO> getTransferDetailsFromReferenceId(@PathVariable String referenceId) {
        return new ResponseEntity<>(fundTranferService.getFundTranferDetailsFromReferenceId(referenceId), HttpStatus.OK);
    }
    @GetMapping
    public ResponseEntity<List<FundTranferDTO>> getAllTransfersByAccountId(@RequestParam String accountId) {
        return new ResponseEntity<>(fundTranferService.getAllFundTranferByAccountId(accountId), HttpStatus.OK);
    }

}
