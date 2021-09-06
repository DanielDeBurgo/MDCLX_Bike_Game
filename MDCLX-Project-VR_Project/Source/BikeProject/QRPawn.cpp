// Fill out your copyright notice in the Description page of Project Settings.

#include "QRPawn.h"
#include "Runtime/Engine/Classes/Engine/Engine.h"
#include "EngineGlobals.h"
#include "BikePawn.h"

// Sets default values
AQRPawn::AQRPawn()
{
 	// Set this pawn to call Tick() every frame.  You can turn this off to improve performance if you don't need it.
	PrimaryActorTick.bCanEverTick = true;

	url = "";

	// Create socket & root component
	RootComponent = CreateDefaultSubobject<USceneComponent>("RootComponent");
	socketIOClientComponent = CreateDefaultSubobject<USocketIOClientComponent>("SocketComponent");

	// Create and attach camera (using reflection to be seen in the engine)
	cameraComponent = CreateDefaultSubobject<UCameraComponent>("CameraComponent");
	cameraComponent->SetupAttachment(RootComponent);
	cameraComponent->SetRelativeLocation(FVector(0.0f, 0.0f, 0.0f));

	websiteComponent = CreateDefaultSubobject<UWidgetComponent>("WebpageComponent");
	websiteComponent->SetupAttachment(RootComponent);
	websiteComponent->SetRelativeLocation(FVector(250.0f, 0.0f, 0.0f));
	websiteComponent->SetRelativeRotation(FRotator(0.0f, 180.0f, 0.0f));

	static ConstructorHelpers::FClassFinder<UUserWidget> widget(TEXT("/Game/QR/BP_QR_Widget"));
	websiteComponent->SetWidgetClass(widget.Class);

	socketIOClientComponent->AddressAndPort = FString("http://192.168.0.11:3001");	
	socketIOClientComponent->bShouldAutoConnect = false;

}

// Called when the game starts or when spawned
void AQRPawn::BeginPlay()
{
	Super::BeginPlay();

	if (!url.IsEmpty())
	{
		socketIOClientComponent->AddressAndPort = url;

		for (TActorIterator<ABikePawn> it(GetWorld()); it; ++it)
		{
			bikePawn = *it;

			// Sets the pawn's socket to be a pointer to this socket
			bikePawn->socketIOClientComponent = this->socketIOClientComponent;
			break;
		}
	}
}


// Called every frame
void AQRPawn::Tick(float DeltaTime)
{
	Super::Tick(DeltaTime);
}

// Called to bind functionality to input
void AQRPawn::SetupPlayerInputComponent(UInputComponent* PlayerInputComponent)
{
	Super::SetupPlayerInputComponent(PlayerInputComponent);

}

