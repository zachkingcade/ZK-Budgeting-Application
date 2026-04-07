export interface ILogoutUserRequestDto {
  username: string;
  sessionToken: string;
}

export type LogoutUserRequestDto = ILogoutUserRequestDto;
