import { Component } from '@angular/core';
import {UserService} from "../../service/user.service";
import {WebauthnService} from "../../service/webauthn.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {

  constructor(public userService : UserService) {
  }

  public logout(){
    this.userService.logout();
  }


}
