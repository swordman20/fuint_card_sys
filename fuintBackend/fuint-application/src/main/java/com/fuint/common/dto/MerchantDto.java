package com.fuint.common.dto;

import java.util.List;

import com.fuint.repository.model.MtMerchant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchantDto {
    MtMerchant mtMerchant;
    List<StoreDto> storeDtos;
}
