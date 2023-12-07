export class UpdateUserSettingsDto {
  constructor(
    public email: string,
    public currentPassword: string,
    public newPassword: string
  ) {}
}
