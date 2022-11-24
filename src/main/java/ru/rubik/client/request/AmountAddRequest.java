package ru.rubik.client.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AmountAddRequest {
    private Integer id;
    private Long amount;
}
