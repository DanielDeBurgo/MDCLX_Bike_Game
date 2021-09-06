// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "GameFramework/Pawn.h"
#include "SocketIOClientComponent.h"
#include "EngineUtils.h"
#include "Engine.h"
#include "BikePawn.generated.h"

UCLASS()
class BIKEPROJECT_API ABikePawn : public APawn
{
	GENERATED_BODY()

public:
	// Sets default values for this pawn's properties
	ABikePawn();

	UPROPERTY(EditAnywhere, BlueprintReadWrite)
	USocketIOClientComponent* socketIOClientComponent;

protected:
	// Called when the game starts or when spawned
	virtual void BeginPlay() override;

public:	
	// Called every frame
	virtual void Tick(float DeltaTime) override;

	// Called to bind functionality to input
	virtual void SetupPlayerInputComponent(class UInputComponent* PlayerInputComponent) override;
};
