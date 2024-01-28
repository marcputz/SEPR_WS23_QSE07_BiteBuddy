import {Component} from '@angular/core';
import {InventoryListDto} from "../../dtos/InventoryListDto";
import {InventoryIngredientDto} from "../../dtos/InventoryIngredientDto";
import {ToastrService} from "ngx-toastr";
import {MenuPlanService} from "../../services/menuplan.service";
import {Router} from "@angular/router";
import {ErrorHandler} from "../../services/errorHandler";

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.scss']
})
export class InventoryComponent {
  inventory: InventoryListDto = {
    missing: [],
    available: []
  }

  constructor(
    private service: MenuPlanService,
    private notification: ToastrService,
    private router: Router,
    private errorHandler: ErrorHandler
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
    this.service.getInventory().subscribe({
      next: value => {
        this.inventory = value;
        this.notification.success("Loaded inventory successfully");
      },
      error: error => {
        let errorDto = this.errorHandler.getErrorObject(error);
        this.errorHandler.handleApiError(errorDto);

        console.error(error);
      }
    });
  }

  public markAsChecked(inv: InventoryIngredientDto) {
    const indexToRemove = this.inventory.missing.indexOf(inv);
    this.inventory.missing.splice(indexToRemove, 1);
    inv.inventoryStatus = true;
    this.inventory.available.push(inv);

    this.service.updateInventoryIngredient(inv).subscribe({
      next: data => {

      },
      error: err => {
        let errorDto = this.errorHandler.getErrorObject(err);
        this.errorHandler.handleApiError(errorDto);
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
      },
      error: err => {
        let errorDto = this.errorHandler.getErrorObject(err);
        this.errorHandler.handleApiError(errorDto);
      }
    });
  }

  public formatInventoryIngredient(ingred: InventoryIngredientDto): string {
    if (ingred != null) {
      if (ingred.unit == null && ingred.amount != -1) {
        return ingred.amount.toString() + " " + ingred.name;
      } else if (ingred.unit == null || ingred.amount == -1) {
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

  public ngOnInit() {
    this.reload();
  }
}
