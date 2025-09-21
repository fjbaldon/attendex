package com.github.fjbaldon.attendex.backend.mapper;

import com.github.fjbaldon.attendex.backend.dto.ScannerResponse;
import com.github.fjbaldon.attendex.backend.model.Scanner;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScannerMapper {
    ScannerResponse toScannerResponse(Scanner scanner);
}
