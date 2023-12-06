export class UpdateUserSettingsDto {
  constructor(
    public email: string,
    public nickname: string,
    public password: string
  ) {}
}
