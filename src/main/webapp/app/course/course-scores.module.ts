import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ArtemisSharedModule } from 'app/shared/shared.module';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { MomentModule } from 'ngx-moment';
import { CourseScoresComponent } from './course-scores.component';
import { SortByModule } from 'app/shared/pipes/sort-by.module';

const ENTITY_STATES = [
    {
        path: 'course/:courseId/dashboard',
        component: CourseScoresComponent,
        data: {
            authorities: ['ROLE_ADMIN', 'ROLE_INSTRUCTOR', 'ROLE_TA'],
            pageTitle: 'instructorDashboard.title',
        },
        canActivate: [UserRouteAccessService],
    },
];

@NgModule({
    imports: [ArtemisSharedModule, MomentModule, RouterModule.forChild(ENTITY_STATES), SortByModule],
    declarations: [CourseScoresComponent],
})
export class ArtemisCourseScoresModule {}