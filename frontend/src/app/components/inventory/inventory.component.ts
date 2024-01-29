import {Component, OnInit} from '@angular/core';
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
export class InventoryComponent implements OnInit {
  maxRows = 20;
  maxRowsAvailable = 20;
  inventory: InventoryListDto = {
    missing: [],
    available: []
  }
  showCreateDialog = false;

  compareInventoryIngredients = (a: InventoryIngredientDto, b: InventoryIngredientDto): number => {
    // Compare based on detailedName
    const detailedNameComparison = (a.detailedNamed || '').localeCompare(b.detailedNamed || '');
    if (detailedNameComparison !== 0) {
      return detailedNameComparison;
    }

    // If detailedNamed is the same, compare based on name
    return (a.name || '').localeCompare(b.name || '');
  };

  constructor(
    private service: MenuPlanService,
    private notification: ToastrService,
    private router: Router,
    private errorHandler: ErrorHandler
  ) {
  }

  public getInventory() {
    this.service.getInventory().subscribe({
      next: value => {
        this.inventory = value;

        if (this.inventory !== null) {
          if (this.inventory.missing !== null) {
            this.inventory.missing.sort(this.compareInventoryIngredients);
          } else {
            this.inventory.missing = [];
          }

          if (this.inventory.available !== null) {
            this.inventory.available.sort(this.compareInventoryIngredients);
          } else {
            this.inventory.available = [];
          }

          if (this.inventory.missing.length > 20) {
            this.maxRows = this.inventory.missing.length / 2;
          }

          if (this.inventory.available.length > 20) {
            this.maxRowsAvailable = this.inventory.available.length / 2;
          }

          this.notification.success("Loaded inventory successfully");
        } else {
          this.inventory = {
            missing: [],
            available: []
          }
        }
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
        return (ingred.amount % 1 !== 0 ? ingred.amount.toFixed(1) : ingred.amount.toString()) + " "
          + ingred.unit + ", " + ingred.name;
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

  onMenuPlanSubmit(): void {
    this.showCreateDialog = false;
    this.ngOnInit();
  }

  protected readonly Math = Math;
}
