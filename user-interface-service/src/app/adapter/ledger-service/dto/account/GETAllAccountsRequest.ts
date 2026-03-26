import { AccountFilterObject } from "./AccountFilterObject";
import { SortObject } from "../SortObject";

export interface GETAllAccountsRequest {
  sort?: SortObject;
  filters?: AccountFilterObject;
}