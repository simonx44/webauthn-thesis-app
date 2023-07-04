import {Component, OnInit} from '@angular/core';
import base64url from "base64url";
import {Buffer} from 'buffer';
import {decodeAllSync}  from "cbor";

const result = "{\"type\":\"public-key\",\"id\":\"vnXFqCA9zXMmOCX-CGQOyqQdqsEgjTHVyAB7KjDviZURmSPL0nJ6qDth1fMRck4k\",\"response\":{\"attestationObject\":\"o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVjCSZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2PFAAAABAAAAAAAAAAAAAAAAAAAAAAAML51xaggPc1zJjgl_ghkDsqkHarBII0x1cgAeyow74mVEZkjy9Jyeqg7YdXzEXJOJKUBAgMmIAEhWCC-dcWoID3NcyY4Jf4ISJtDCITCtlVCAb1j6eU30LTAuiJYIFmxl1QyS3ulCfTdLuatkh5YuWsdg_A1wHXBsQy2zW4CoWtjcmVkUHJvdGVjdAI\",\"clientDataJSON\":\"eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiWHhTTmRKLWF1bGZOdVRnd1pRRzh1MzY2WWlxbk1NSTJRYnEtR1BMR3RaYyIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6NDIwMCIsImNyb3NzT3JpZ2luIjpmYWxzZX0\",\"transports\":[\"nfc\",\"usb\"]},\"clientExtensionResults\":{\"credProps\":{\"rk\":true}}}";


@Component({
  selector: 'app-auth-create-result-parser',
  templateUrl: './auth-create-result-parser.component.html',
  styleUrls: ['./auth-create-result-parser.component.scss']
})
export class AuthCreateResultParserComponent implements OnInit {



  private response: any;

  ngOnInit(): void {

    this.response = JSON.parse(result);
    this.parseAttestationObject();
  }

  private parseAttestationObject(){

    try {
      let attestationObjectBuffer = Buffer.from(this.response.response.attestationObject,'base64');

      console.log(attestationObjectBuffer);
      let ctapMakeCredResp        = decodeAllSync(attestationObjectBuffer)[0];

       console.log(ctapMakeCredResp);
    } catch (e) {
      console.log(e);

    }



  }









}



