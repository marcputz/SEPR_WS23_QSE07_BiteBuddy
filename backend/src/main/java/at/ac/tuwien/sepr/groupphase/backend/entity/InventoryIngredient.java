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
    private String name;

    @Column(nullable = false)
    private Long menuPlanId;

    @Column(nullable = false)
    private Long basicIngredientId;

    @Column(nullable = true)
    private String detailedIngredientName;

    @Column(nullable = true)
    private Float amount;

    @Column(nullable = true)
    private FoodUnit unit;

    @Column(nullable = false)
    private boolean inventoryStatus;

    public InventoryIngredient() {
    }

    public InventoryIngredient(String name, Long menuPlanId, Long basicIngredientId, String detailedIngredientName, Float amount, FoodUnit unit, boolean inventoryStatus) {
        this.name = name;
        this.menuPlanId = menuPlanId;
        this.basicIngredientId = basicIngredientId;
        this.detailedIngredientName = detailedIngredientName;
        this.amount = amount;
        this.unit = unit;
        this.inventoryStatus = inventoryStatus;
    }

    public Long getId() {
        return id;
    }

    public Long getMenuPlanId() {
        return menuPlanId;
    }

    public InventoryIngredient setMenuPlanId(Long menuPlanId) {
        this.menuPlanId = menuPlanId;
        return this;
    }

    public Long getBasicIngredientId() {
        return basicIngredientId;
    }

    public InventoryIngredient setBasicIngredientId(Long basicIngredientId) {
        this.basicIngredientId = basicIngredientId;
        return this;
    }

    public String getDetailedIngredientName() {
        return detailedIngredientName;
    }

    public InventoryIngredient setDetailedIngredientName(String detailedIngredientName) {
        this.detailedIngredientName = detailedIngredientName;
        return this;
    }

    public Float getAmount() {
        return amount;
    }

    public InventoryIngredient setAmount(Float amount) {
        this.amount = amount;
        return this;
    }

    public boolean getInventoryStatus() {
        return inventoryStatus;
    }

    public FoodUnit getUnit() {
        return unit;
    }

    public InventoryIngredient setUnit(FoodUnit unit) {
        this.unit = unit;
        return this;
    }

    public InventoryIngredient setInventoryStatus(boolean inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
        return this;
    }

    /**
     * This is needed since we want a custom way to index ingredients with just name and FoodUnit
     * @return
     */
    public String getFridgeStringIdentifier() {
        return detailedIngredientName + " " + (getUnit() != null ? getUnit().toString() : "");
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
                && Objects.equals(other.basicIngredientId, basicIngredientId) && Objects.equals(other.amount, amount)
                && Objects.equals(other.inventoryStatus, inventoryStatus) && Objects.equals(other.unit, unit)
                && Objects.equals(other.detailedIngredientName, detailedIngredientName);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, menuPlanId, basicIngredientId, detailedIngredientName, amount, inventoryStatus, unit);
    }

    public String getName() {
        return name;
    }

    public InventoryIngredient setName(String name) {
        this.name = name;
        return this;
    }
}
