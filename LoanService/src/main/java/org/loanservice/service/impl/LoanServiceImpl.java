package org.loanservice.service.impl;

import org.loanservice.exception.LoanAlreadyExistsException;
import org.loanservice.exception.ResourceNotFound;
import org.loanservice.model.StatusCode;
import org.loanservice.model.dto.LoanDTO;
import org.loanservice.model.entity.Loan;
import org.loanservice.model.mapper.LoanMapper;
import org.loanservice.repository.LoanRepository;
import org.loanservice.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class LoanServiceImpl implements LoanService {
    @Autowired
    private LoanRepository loanRepository;

    private final LoanMapper loanMapper = new LoanMapper();
    @Override
    public void createLoan(String mobileNumber) {
        Optional<Loan> loans = loanRepository.findByMobileNumber(mobileNumber);
        if(loans.isPresent()){
            throw new LoanAlreadyExistsException("Loan already exists for the registered mobile number "+mobileNumber);
        }
        loanRepository.save(createNewLoan(mobileNumber));
    }
    /**
     * @param mobileNumber - Mobile Number of the Customer
     * @return the new loan details
     */
    private Loan createNewLoan(String mobileNumber){
        Loan newLoan = new Loan();
        long randomLoanNumber = 100000000000L + new Random().nextInt(900000000);
        newLoan.setLoanNumber(Long.toString(randomLoanNumber));
        newLoan.setMobileNumber(mobileNumber);
        newLoan.setLoanType(StatusCode.HOME_LOAN);
        newLoan.setTotalLoan(StatusCode.NEW_LOAN_LIMIT);
        newLoan.setAmountPaid(0);
        newLoan.setOutstandingAmount(StatusCode.NEW_LOAN_LIMIT);
        return newLoan;
    }

    /**
     * @param mobileNumber - Input mobile Number
     * @return Loan Details based on a given mobileNumber
     */
    @Override
    public LoanDTO fetchLoan(String mobileNumber) {
        Optional<Loan> loans = loanRepository.findByMobileNumber(mobileNumber);
        if(loans.isEmpty()){
            throw new ResourceNotFound("Loan","mobileNumber",mobileNumber);
        }
        return loanMapper.convertToDto(loans.get());
    }

    /**
     * @param loansDto - LoansDto Object
     * @return boolean indicating if the update of card details is successful or not
     */
    @Override
    public boolean updateLoan(LoanDTO loansDto) {
        Loan loans = loanRepository.findByLoanNumber(
                loansDto.getLoanNumber()
        ).orElseThrow(
                ()-> new ResourceNotFound("Loan","LoanNumber",loansDto.getLoanNumber())
        );
        Loan updatedLoan = loanMapper.convertToEntity(loansDto);
        updatedLoan.setLoanNumber(loans.getLoanNumber());
        updatedLoan.setLoanId(loans.getLoanId());
        loanRepository.save(updatedLoan);
        return true;
    }

    /**
     * @param mobileNumber - Input Mobile Number
     * @return boolean indicating if the delete of loan details is successful or not
     */
    @Override
    public boolean deleteLoan(String mobileNumber) {
        Loan loans = loanRepository.findByMobileNumber(mobileNumber).orElseThrow(
                () -> new ResourceNotFound("Loan", "mobileNumber", mobileNumber)
        );
        loanRepository.deleteById(loans.getLoanId());
        return true;
    }
}
