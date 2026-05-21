export interface IRefreshLoginResponseDto {
  accessToken: string | null;
  accessTokenCreatedAt: string | null;
  AccessTokenExpiresAt: string | null;
  roles: string[];
}

export type RefreshLoginResponseDto = IRefreshLoginResponseDto;
