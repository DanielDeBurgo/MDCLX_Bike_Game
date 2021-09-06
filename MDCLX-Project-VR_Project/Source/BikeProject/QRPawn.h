// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "GameFramework/Pawn.h"
#include "SocketIOClientComponent.h"
#include "Camera/CameraComponent.h"
#include "WidgetComponent.h"
#include "BikePawn.h"
#include "EngineUtils.h"
#include "QRPawn.generated.h"


UCLASS()
class BIKEPROJECT_API AQRPawn : public APawn
{
	GENERATED_BODY()

public:
	// Sets default values for this pawn's properties
	AQRPawn();

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	UCameraComponent* cameraComponent;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	UWidgetComponent* websiteComponent;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	USocketIOClientComponent* socketIOClientComponent;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	ABikePawn* bikePawn;

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	FString url;

// 	UFUNCTION(BlueprintNativeEvent)
// 	void changeBikeColor();
// 	virtual void changeBikeColor_Implementation();
// 	void changeBikeModel();
// 	virtual void changeBikeModel_Implementation();
// 	void changeSpokeColour();
// 	virtual void changeSpokeColour_Implementation();
// 	void changeMiscModel();
// 	virtual void changeMiscModel_Implementation();
// 	void changeMiscColour();
// 	virtual void changeMiscColour_Implementation();
// 	void changeMiscPosition();
// 	virtual void changeMiscPosition_Implementation();

protected:
	// Called when the game starts or when spawned
	virtual void BeginPlay() override;

public:	
	// Called every frame
	virtual void Tick(float DeltaTime) override;

	// Called to bind functionality to input
	virtual void SetupPlayerInputComponent(class UInputComponent* PlayerInputComponent) override;

};
