import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {catchError, concatMap, of, throwError} from "rxjs";


@Injectable({
    providedIn: 'root'
})
export class UserService {

    private static readonly BASE_URL = "http://localhost:8080";

    private static readonly DEFAULT_HEADERS = {withCredentials: true};
    private _isUserLoggedIn: boolean = false;
    private _username: string = "";

    constructor(private client: HttpClient, private router: Router) {
    }


    get isUserLoggedIn(): boolean {
        return this._isUserLoggedIn;
    }

    set isUserLoggedIn(value: boolean) {
        this._isUserLoggedIn = value;
    }

    get username(): string {
        return this._username;
    }

    set username(value: string) {
        this._username = value;
    }

    public logout() {
        const url = `${UserService.BASE_URL}/auth/logout`;
        this.client.get<any>(url, UserService.DEFAULT_HEADERS).subscribe((res) => {
            this.isUserLoggedIn = false;
            this.router.navigateByUrl("/auth");
        })
    }

    public getUserInfo() {
        const url = `${UserService.BASE_URL}/auth/getUserInfo`;
        return this.client.get<any>(url, UserService.DEFAULT_HEADERS)
            .pipe(
                catchError(() => {
                    this.isUserLoggedIn = false;
                    this.username = "";
                    this.router.navigateByUrl("/auth");
                    return throwError("");
                }), concatMap(res => {
                    this.isUserLoggedIn = true;
                    this.username = res.username;
                    this.router.navigateByUrl("/home");
                    return of(res);
                }));
    }


}
