export class UpdateUserSettingsDto {
  constructor(
    public nickname: string,
    public userPicture: number[]
  ) {
  }
}
