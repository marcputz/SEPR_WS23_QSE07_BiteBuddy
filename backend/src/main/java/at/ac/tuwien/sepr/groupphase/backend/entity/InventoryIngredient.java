package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class InventoryIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long menuPlanId;

    @Column(nullable = false)
    private Long ingredientId;

    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private boolean inventoryStatus;

    public Long getMenuPlanId() {
        return menuPlanId;
    }

    public InventoryIngredient setMenuPlanId(Long menuPlanId) {
        this.menuPlanId = menuPlanId;
        return this;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public InventoryIngredient setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
        return this;
    }

    public float getAmount() {
        return amount;
    }

    public InventoryIngredient setAmount(float amount) {
        this.amount = amount;
        return this;
    }

    public boolean isInventoryStatus() {
        return inventoryStatus;
    }

    public InventoryIngredient setInventoryStatus(boolean inventoryStatus) {
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
            InventoryIngredient other = (InventoryIngredient) o;
            return Objects.equals(other.id, id) && Objects.equals(other.menuPlanId, menuPlanId)
                && Objects.equals(other.ingredientId, ingredientId) && Objects.equals(other.amount, amount)
                && Objects.equals(other.inventoryStatus, inventoryStatus);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, menuPlanId, ingredientId, amount, inventoryStatus);
    }
}
