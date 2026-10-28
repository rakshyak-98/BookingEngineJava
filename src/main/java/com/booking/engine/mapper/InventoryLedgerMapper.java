package com.booking.engine.mapper;

import com.booking.engine.dto.LedgerEntryDTO;
import com.booking.engine.entity.InventoryLedgerEntry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryLedgerMapper {

    LedgerEntryDTO toDto(InventoryLedgerEntry entry);
}
