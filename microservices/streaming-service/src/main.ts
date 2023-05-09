import {NestFactory} from '@nestjs/core';
import {AppModule} from './app.module';
import {ConfigService} from '@nestjs/config';

async function bootstrap() {
    const app = await NestFactory.create(AppModule);

    const activeProfile = process.env.ACTIVE_PROFILE || 'dev';

    const configService = app.get(ConfigService);
    const port = configService.get(`${activeProfile}.http.port`) || 3000

    await app.listen(port);
}

bootstrap();
