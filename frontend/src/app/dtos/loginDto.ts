export class LoginDto {
  constructor(
    public email: string,
    public passwordEncoded: string
  ) {}
}
