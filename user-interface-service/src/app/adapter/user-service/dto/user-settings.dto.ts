export interface IUserSettingDto {
  settingId: number;
  settingName: string;
  settingValue: string;
}

export interface ICreateUserSettingRequest {
  settingName: string;
  settingValue: string;
}
