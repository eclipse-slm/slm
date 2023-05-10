package org.eclipse.slm.notification_service.service.client.test;

//@SpringBootTest(classes = NotificationServiceClient.class)
//public class TestNotificationServiceClient {
//    @Autowired
//    NotificationServiceClient client;
//
//    @Ignore
//    @Test
//    public void testPostJobObserver() {
//        String userUuid = "cf10fb2e-926f-44a4-9b70-adcb9bcd10c5";
//        int jobId = 8719;
//        JobTargets jobTarget = JobTargets.RESOURCE;
//        JobGoals jobGoal = JobGoals.CREATE;
//
////        Mono<ResponseEntity<Void>> responseEntity = client.postJobObserver(jobId, jobTarget, jobGoal);
//
////        Assertions.assertThat(responseEntity..getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
//    }
//
//    @Ignore
//    @Test
//    public void testPostNotification() {
//        String userUuid = "cf10fb2e-926f-44a4-9b70-adcb9bcd10c5";
//        Category category = Category.Resources;
//        String text = category.name() + "has been created.";
//
//        ResponseEntity<Void> responseEntity = client.postNotification(userUuid, category, text);
//
//        Assertions.assertThat(responseEntity.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
//    }
//
//}
