export interface ILoginUserResponseDto {
  username: string;
  sessionToken: string;
  sessionCreatedAt: string;
  sessionExpiresAt: string;
  accessToken: string;
  accessTokenCreatedAt: string;
  AccessTokenExpiresAt: string;
}

export type LoginUserResponseDto = ILoginUserResponseDto;
