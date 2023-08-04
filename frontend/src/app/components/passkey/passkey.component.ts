import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PasskeyT} from "../../types";

@Component({
  selector: 'app-passkey',
  templateUrl: './passkey.component.html',
  styleUrls: ['./passkey.component.scss']
})
export class PasskeyComponent implements OnInit {

  @Input() deleteable: boolean = false;
  @Input() data: PasskeyT | undefined;

  @Output() deletePasskeyEvent = new EventEmitter<number>();

  currentIndex = 0;

  authDataToRender: any = {};


  ngOnInit(): void {

    const authData = this.data?.attestationObject?.authData;

    this.authDataToRender = {
      "type": this.getDisplayedPasskeyName(this.data),
      "count": this.data?.count.toString() ?? "",
      "createdOn": this.transformDate(this.data?.createdOn ?? ""),
      "lastUsedOn": this.transformDate(this.data?.lastUpdatedOn ?? ""),
      "model" : "-"
    }

  }

  private getDisplayedPasskeyName(passkey: PasskeyT | undefined) {

    let name = "";

    if (!passkey)
      return "passkey";

    const {isBackedUp, isDiscoverable, isBackupEligible} = passkey;

    if (isDiscoverable) {
      //name += isBackupEligible ? "Synchronizable " : "Device bound ";
      name += isBackedUp ? "Multi Device Credential" : "Single Device Credential"
    } else {
      name += "Server side credential"
    }
    return name;

  }

  private transformDate(dateString: string) {
    const date = new Date(dateString);
    if (!date)
      return "-";

    return `${date.toDateString()} ${date.toLocaleTimeString()}`


  }

  public isObject(value: any) {
    return typeof value === "object";
  }


  public deletePasskey() {
    this.deletePasskeyEvent.emit(this.data?.id);
  }


}
