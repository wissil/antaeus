## Development description

The main motivation was to create a sensible solution that would best fit the given requirement. The design was meant to be highly scalable and testable. This was achieved through multiple smaller components that work together in order to achieve the wanted behaviour.

#### Introduction to the project
The idea of this step was to build and run the service, and get familiar with it's concepts.

1) The initial goal was to build and run the project, and try to invoke some of the existing API endpoints and verify that everything works as expected.

#### Upgrading the Data Access Layer and the Invoice Service
The idea of this step was to upgrade the DAL and the Invoice Service to be able to fetch the invoices with the desired status.

1) I have added a new endpoint to the API that would fetch all the invoices marked as ```PENDING```. This was made as an empirical test that the solution works, with the flow from the database layer through the invoice service to the API. I have tested the flow through Postman, and in the following steps wrote some unit tests to formally test the new functionalities.

2) At this stage I've already implemented the DAL methods for fetching invoices with the desired ```InvoiceStatus```, so I verified that those methods work by writing the corresponding unit tests.


#### Introducing the asynchronous billing job
The point of this step was to build the basis for billing logic. The idea for the billing logic was to have an asynchronous job that would only take care of the billing on every 1st day of the month, while the rest of the service would be independent of that task. This was meant to be as a background, scheduled job.

For that purpose I took advantage of ```quartz``` libraries that provide support for implementing asynchronous scheduled jobs. This idea went through couple of iterations in Git commits, but it's basic logic didn't change. The basic logic was always to have a ``` BillingService ``` ran as an asynchrnous scheduled job. The design here had to be inline with the ```quartz``` library API, which was to provide the billing service through the scheduler context to the ```BillingJob``` which would execute the service whenever the scheduled job is triggered by the scheduled trigger.


#### Upgrading the service to update the invoices
At this point I'm adding the support to be able to update the invoice. The components affected are the DAL and the ```InvoiceService```. This is needed for the ```BillingService``` to update the invoice status that was successfully charged from ```PENDING``` to ```PAID```.

#### Refactoring the billing logic
At this stage, the service is already working as expected, but I'm trying to enhance the design. For that purpose I'm creating a specialized ```InvoicePaymentExecutor```whose only responsibility is to charge a single invoice. This component is called from the billing that iterates through all the invoices, and calls the executor for each of them. Also, this is the making the foundation to the error handling which is to be implemented as part of the ```InvoicePaymentExecutor``` as well, not to overload the ```BillingService``` with too much logic or dependencies.

#### Adding the handling of various failure scenarios
At this point I've created the two new components. They are the ```SupportService``` and the ```RetryService```. The purpose of the former is to contact the actual human support at the company to take measures for whatever scenario couldn't be handled automatically by the service itself - like when the customer was not found, or the currency of the customer don't match to the currency of the invoice. The purpose of the latter is to implement the retry logic in case of a temporary network error which could be automatically resolved by few retries. The error handlers were added as dependencies to the ```InvoicePaymentExecutor``` where the actual charging logic takes place.
