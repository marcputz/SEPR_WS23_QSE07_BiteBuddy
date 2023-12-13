export class UserSettingsDto {
  constructor(
    public id: number,
    public email: string,
    public nickname: string,
    public createdAt: Date,
    public updatedAt: Date
  ) {}
}
