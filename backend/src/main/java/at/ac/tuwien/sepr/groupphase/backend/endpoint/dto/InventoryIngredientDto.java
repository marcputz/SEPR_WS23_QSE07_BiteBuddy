package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class InventoryIngredientDto {
    @NotNull
    private Long id;

    @NotNull
    private Long menuPlanId;

    @NotNull
    private long ingredientId;

    @NotNull
    private float amount;

    private boolean inventoryStatus;

    public InventoryIngredientDto(Long id, Long menuPlanId, long ingredientId, float amount, boolean inventoryStatus) {
        this.id = id;
        this.menuPlanId = menuPlanId;
        this.ingredientId = ingredientId;
        this.amount = amount;
        this.inventoryStatus = inventoryStatus;
    }

    public InventoryIngredientDto() {
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
