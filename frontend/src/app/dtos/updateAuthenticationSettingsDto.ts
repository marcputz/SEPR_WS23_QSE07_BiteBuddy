export class UpdateAuthenticationSettingsDto {
  constructor(
    public email: string,
    public currentPassword: string,
    public newPassword: string
  ) {
  }
}
