// Fill out your copyright notice in the Description page of Project Settings.

#pragma once

#include "CoreMinimal.h"
#include "Blueprint/UserWidget.h"
#include "QRWidget.generated.h"

/**
 * 
 */
UCLASS()
class BIKEPROJECT_API UQRWidget : public UUserWidget
{
	GENERATED_BODY()

	public:
	UPROPERTY(EditAnywhere, BlueprintReadWrite, Category = "SocketIORequirements")
	FString url;
	
};
