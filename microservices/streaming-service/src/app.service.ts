import {Injectable, StreamableFile} from '@nestjs/common';
import {ConfigService} from '@nestjs/config';
import * as fs from 'fs';

@Injectable()
export class AppService {
  constructor(private readonly configService: ConfigService) {}

  // TODO: rewrite this method
  getStreamingMedia(fileName) {
    const filePath = this.configService.get('test.m3u8.location') + fileName;
    const file = fs.createReadStream(filePath);
    return new StreamableFile(file);
  }
}
