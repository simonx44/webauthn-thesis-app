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

  authDataToRender: Record<string, any>  = {};


  ngOnInit(): void {

    const authData = this.data?.attestationObject?.authData;

    const dataToRender = {

      "aaguid": authData?.attestedCredData.aaguid,
      "Counter": authData?.counter,
      "rpIdHash": authData?.rpIdHash,
      "flags": authData?.flags
    }
    this.authDataToRender = dataToRender;
  }

  public isObject(value: any){
    return typeof value === "object";
  }


  public deletePasskey() {
    this.deletePasskeyEvent.emit(this.data?.id);
  }







}
