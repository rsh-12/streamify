import {Controller, Get, Param, StreamableFile} from '@nestjs/common';
import {AppService} from './app.service';

@Controller()
export class AppController {
    constructor(private readonly appService: AppService) {
    }

    // Use 0.m3u8 file name for testing
    @Get(':fileName')
    getFileStream(@Param() params: any): StreamableFile {
        return this.appService.getStreamingMedia(params.fileName);
    }
}
