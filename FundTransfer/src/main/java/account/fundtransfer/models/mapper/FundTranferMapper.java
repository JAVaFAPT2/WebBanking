package account.fundtransfer.models.mapper;


import account.fundtransfer.models.dto.FundTranferDTO;
import account.fundtransfer.models.entity.FundTranfer;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

public class FundTranferMapper extends BaseMapper<FundTranfer, FundTranferDTO> {
    @Override
    public FundTranfer convertToEntity(FundTranferDTO dto, Object... args) {
        FundTranfer account = new FundTranfer();
        if(!Objects.isNull(dto)){
            BeanUtils.copyProperties(dto, account);
        }
        return account;
    }

    @Override
    public FundTranferDTO convertToDto(FundTranfer entity, Object... args) {
        FundTranferDTO accountDto = new FundTranferDTO();
        BeanUtils.copyProperties(entity, accountDto);
        return accountDto;
    }
}
