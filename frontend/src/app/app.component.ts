import {Component, OnInit} from '@angular/core';
import {UserService} from "./service/user.service";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {

    public isLoading: boolean = true;


    constructor(private userService: UserService) {
    }

    ngOnInit(): void {
        this.isLoading = true;
        this.userService.getUserInfo().subscribe((data) => {
            this.isLoading = false;
        }, error => {
            this.isLoading = false;
        })
    }


}

