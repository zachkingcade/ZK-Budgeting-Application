export interface IRefreshLoginResponseDto {
  accessToken: string | null;
  accessTokenCreatedAt: string | null;
  AccessTokenExpiresAt: string | null;
}

export type RefreshLoginResponseDto = IRefreshLoginResponseDto;
