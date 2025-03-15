package org.loanservice.model.mapper;

import org.loanservice.model.dto.LoanDTO;
import org.loanservice.model.entity.Loan;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

public class LoanMapper extends BaseMapper<Loan, LoanDTO> {

    @Override
    public Loan convertToEntity(LoanDTO dto, Object... args) {
        Loan Loan = new Loan();
        if(!Objects.isNull(dto)){
            BeanUtils.copyProperties(dto, Loan);
        }
        return Loan;
    }

    @Override
    public LoanDTO convertToDto(Loan entity, Object... args) {
        LoanDTO LoanDTO = new LoanDTO();
        if(!Objects.isNull(entity)){
            BeanUtils.copyProperties(entity, LoanDTO);
        }
        return LoanDTO;
    }
}
