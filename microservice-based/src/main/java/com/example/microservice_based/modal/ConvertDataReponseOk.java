package com.example.microservice_based.modal;

import java.util.List;

import lombok.Data;

@Data
public class ConvertDataReponseOk {

    private int total;
    private List<SinhVienModal> data;
}
