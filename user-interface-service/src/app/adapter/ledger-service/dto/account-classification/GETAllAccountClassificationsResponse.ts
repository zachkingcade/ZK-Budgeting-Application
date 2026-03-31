import { AccountClassificationObject } from "./AccountClassificationObject";

export interface IGETAllAccountClassificationsResponse {
  accountClassificationList: AccountClassificationObject[];
}

export type GETAllAccountClassificationsResponse = IGETAllAccountClassificationsResponse;
