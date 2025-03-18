package org.cartservice.model.mapper;


import org.cartservice.model.dto.CardsDTO;
import org.cartservice.model.entity.Cards;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CardMapper extends BaseMapper<Cards, CardsDTO> {

    @Override
    public Cards convertToEntity(CardsDTO dto, Object... args) {
        Cards Cards = new Cards();
        if(!Objects.isNull(dto)){
            BeanUtils.copyProperties(dto, Cards);
        }
        return Cards;
    }

    @Override
    public CardsDTO convertToDto(Cards entity, Object... args) {
        CardsDTO cardsDTO = new CardsDTO();
        if(!Objects.isNull(entity)){
            BeanUtils.copyProperties(entity, cardsDTO);
        }
        return cardsDTO;
    }
}
