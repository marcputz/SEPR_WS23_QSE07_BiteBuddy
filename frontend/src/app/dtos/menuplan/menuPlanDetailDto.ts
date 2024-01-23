import {MenuPlanContentDetailDto} from "./menuPlanContentDetailDto";

export interface MenuPlanDetailDto {
  userId: number;
  profileId: number;
  profileName: string;
  fromTime: string;
  untilTime: string;
  numDays: number;
  contents: Set<MenuPlanContentDetailDto>;

}
