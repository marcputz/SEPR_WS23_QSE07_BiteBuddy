package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.FoodUnit;

import java.util.Objects;

public class InventoryIngredientDto {
    private Long id;
    private String name;
    private Long menuPlanId;
    private long ingredientId;
    private Float amount;
    private FoodUnit unit;
    private boolean inventoryStatus;
    private String detailedName;

    public InventoryIngredientDto(Long id, String name, Long menuPlanId, long ingredientId, String detailedName, Float amount, FoodUnit unit, boolean inventoryStatus) {
        this.id = id;
        this.name = name;
        this.menuPlanId = menuPlanId;
        this.detailedName = detailedName;
        this.ingredientId = ingredientId;
        this.amount = amount;
        this.unit = unit;
        this.inventoryStatus = inventoryStatus;
    }

    public InventoryIngredientDto() {
    }

    public String getName() {
        return name;
    }

    public InventoryIngredientDto setName(String name) {
        this.name = name;
        return this;
    }

    public Long getId() {
        return id;
    }

    public InventoryIngredientDto setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getMenuPlanId() {
        return menuPlanId;
    }

    public InventoryIngredientDto setMenuPlanId(Long menuPlanId) {
        this.menuPlanId = menuPlanId;
        return this;
    }

    public long getIngredientId() {
        return ingredientId;
    }

    public InventoryIngredientDto setIngredientId(long ingredientId) {
        this.ingredientId = ingredientId;
        return this;
    }

    public float getAmount() {
        return amount;
    }

    public InventoryIngredientDto setAmount(float amount) {
        this.amount = amount;
        return this;
    }

    public FoodUnit getUnit() {
        return unit;
    }

    public InventoryIngredientDto setUnit(FoodUnit unit) {
        this.unit = unit;
        return this;
    }

    public boolean isInventoryStatus() {
        return inventoryStatus;
    }

    public InventoryIngredientDto setInventoryStatus(boolean inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            InventoryIngredientDto other = (InventoryIngredientDto) o;
            return Objects.equals(other.id, id) && Objects.equals(other.menuPlanId, menuPlanId)
                && Objects.equals(other.ingredientId, ingredientId) && Objects.equals(other.amount, amount)
                && Objects.equals(other.inventoryStatus, inventoryStatus);
        }
    }

    @Override
    public String toString() {
        return "InventoryIngredientDto{"
            + "id=" + id
            + ", ingredientId=" + ingredientId
            + ", menuPlanId=" + menuPlanId
            + ", amount=" + amount
            + ", inventoryStatus=" + inventoryStatus
            + "}";
    }
}
