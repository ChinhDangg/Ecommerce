package dev.ecommerce.product.metadata;

import java.util.ArrayList;
import java.util.List;

public class LaptopFilters extends ProductFilters {

    private final FilterFieldMetadata displaySize = new FilterFieldMetadata("displaySize", "Display Size");
    private final FilterFieldMetadata processor = new FilterFieldMetadata("processor", "Processor");
    private final FilterFieldMetadata ram = new FilterFieldMetadata("ram", "RAM");
    private final FilterFieldMetadata monitorResolution = new FilterFieldMetadata("monitorResolution", "Monitor Resolution");
    private final FilterFieldMetadata storageCapacity = new FilterFieldMetadata("storageCapacity", "Storage Capacity");
    private final FilterFieldMetadata operatingSystem = new FilterFieldMetadata("operatingSystem", "Operating System");
    private final FilterFieldMetadata externalPorts = new FilterFieldMetadata("externalPorts", "External Ports");
    private final FilterFieldMetadata model = new FilterFieldMetadata("model", "Model");
    private final FilterFieldMetadata weight = new FilterFieldMetadata("weight", "Weight");
    private final FilterFieldMetadata panelType = new FilterFieldMetadata("panelType", "Panel Type");
    private final FilterFieldMetadata maximumBrightness = new FilterFieldMetadata("maximumBrightness", "Maximum Brightness");
    private final FilterFieldMetadata flashMediaExpansionSlots = new FilterFieldMetadata("expansionSlots", "Flash Media & Expansion Slots");
    private final FilterFieldMetadata aspectRatio = new FilterFieldMetadata("aspectRatio", "Aspect Ratio");
    private final FilterFieldMetadata security = new FilterFieldMetadata("security", "Security");

    @Override
    public List<FilterFieldMetadata> getAllFilterFieldMetadata() {
        List<FilterFieldMetadata> fields = new ArrayList<>(super.getAllFilterFieldMetadata());
        fields.addAll(List.of(
                displaySize,
                processor,
                ram,
                monitorResolution,
                storageCapacity,
                operatingSystem,
                externalPorts,
                model,
                weight,
                panelType,
                maximumBrightness,
                flashMediaExpansionSlots,
                aspectRatio,
                security
        ));
        return fields;
    }
}
