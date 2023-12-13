export class ResetPasswordDto {
  constructor(
    public resetId: string,
    public newPassword: string
  ) {}
}
