package in.healix.modules.inventory.mapper;

import in.healix.modules.inventory.domain.InventoryBatch;
import in.healix.modules.inventory.domain.StockAdjustment;
import in.healix.modules.inventory.domain.StockTransfer;
import in.healix.modules.inventory.web.dto.InventoryBatchDTO;
import in.healix.modules.inventory.web.dto.StockAdjustmentDTO;
import in.healix.modules.inventory.web.dto.StockTransferDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryMapper {

    InventoryBatchDTO toDto(InventoryBatch batch);
    InventoryBatch toEntity(InventoryBatchDTO batchDTO);

    @Mapping(source = "batch.id", target = "batchId")
    StockAdjustmentDTO toDto(StockAdjustment adjustment);

    @Mapping(source = "batchId", target = "batch.id")
    StockAdjustment toEntity(StockAdjustmentDTO adjustmentDTO);

    @Mapping(source = "batch.id", target = "batchId")
    StockTransferDTO toDto(StockTransfer transfer);

    @Mapping(source = "batchId", target = "batch.id")
    StockTransfer toEntity(StockTransferDTO transferDTO);
}
