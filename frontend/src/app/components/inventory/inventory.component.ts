import {Component} from '@angular/core';
import {InventoryListDto} from "../../dtos/InventoryListDto";
import {InventoryIngredientDto} from "../../dtos/InventoryIngredientDto";
import {ToastrService} from "ngx-toastr";
import {MenuPlanService} from "../../services/menuplan.service";

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss']
})
export class InventoryComponent {
  inventory: InventoryListDto

  constructor(
    private service: MenuPlanService,
    private notification: ToastrService,
  ) {
  }

  public createInventory() {
    this.service.createInventory().subscribe({
      next: value => {
        this.notification.success("Created inventory")
        this.reload()
      }
    });
  }

  public getInventory() {
    this.service.getInventory().subscribe(
      (value: InventoryListDto) => {
        this.inventory = value;
        this.notification.success("Loaded inventory successfully");
      }
    )
  }

  public markAsChecked(inv: InventoryIngredientDto) {
    const indexToRemove = this.inventory.missing.indexOf(inv);
    this.inventory.missing.splice(indexToRemove, 1);
    inv.inventoryStatus = true;
    this.inventory.available.push(inv);

    this.service.updateInventoryIngredient(inv).subscribe({
      next: data => {
        console.log("Update was successful");
      },
      error: err => {
        console.log("Update failed");
        this.notification.error("Updating Inventory failed");
      }
    });
  }

  public markAsUnchecked(inv: InventoryIngredientDto) {
    const indexToRemove = this.inventory.available.indexOf(inv);
    this.inventory.available.splice(indexToRemove, 1);
    this.inventory.missing.push(inv);
    inv.inventoryStatus = false;

    this.service.updateInventoryIngredient(inv).subscribe({
      next: data => {
        console.log("Update was successful");
      },
      error: err => {
        console.log("Update failed");
        this.notification.error("Updating Inventory failed");
      }
    });
  }

  public formatInventoryIngredient(ingred: InventoryIngredientDto): string {
    if (ingred != null) {
      if (ingred.unit == null && ingred.amount != null) {
        return ingred.amount.toString() + " " + ingred.name;
      } else if (ingred.unit == null || ingred.amount == null) {
        return ingred.name;
      } else {
        return ingred.name + ", " + ingred.amount.toFixed(1) + " " + ingred.unit;
      }
    }
    return "";
  }

  private reload() {
    this.getInventory();
  }
}
