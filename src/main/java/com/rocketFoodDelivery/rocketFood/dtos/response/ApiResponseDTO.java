package com.rocketFoodDelivery.rocketFood.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponseDTO {
    private String message;
    private Object data;
}
